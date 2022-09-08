
package com.worldfamilyenglish.vegas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.worldfamilyenglish.vegas.domain.BookingStatus;
import com.worldfamilyenglish.vegas.domain.CapLevel;
import com.worldfamilyenglish.vegas.domain.Child;
import com.worldfamilyenglish.vegas.domain.Parent;
import com.worldfamilyenglish.vegas.persistence.IChildRepository;
import com.worldfamilyenglish.vegas.persistence.IParentRepository;
import com.worldfamilyenglish.vegas.types.CreateChildInput;
import com.worldfamilyenglish.vegas.types.CreateParentInput;
import com.worldfamilyenglish.vegas.util.TestDataBuilder;

public class ParentServiceTest {

	private IParentRepository _mockParentRepository;
	private IChildRepository _mockChildRepository;

	private ParentService _serviceUnderTest;

	@BeforeEach
	public void setUp() {
		_mockParentRepository = mock(IParentRepository.class);
		_mockChildRepository = mock(IChildRepository.class);
		
		_serviceUnderTest = new ParentService(_mockParentRepository, _mockChildRepository);
	}
	
	@Test
    void GIVEN_parent_exist_with_child_and_booking_attached_EXPECT_parent_with_child_and_booking_object_returned_from_service() {

        givenThereIsAParentAndChildAndBooking();

        Optional<Parent> foundParent = _serviceUnderTest.getParent("P1478");

        assertThat(foundParent).isPresent();
        assertThat(foundParent.get().getExternalId()).isEqualTo("P1478");

        assertThat(foundParent.get().getChildren()).hasSize(1);
        assertThat(foundParent.get().getChildren().get(0).getExternalId()).isEqualTo("C7895");

        assertThat(foundParent.get().getChildren().get(0).getBookings()).hasSize(1);
        assertThat(foundParent.get().getChildren().get(0).getBookings().get(0).getStatus()).isEqualTo(BookingStatus.BOOKED);
    }

    @Test
    void GIVEN_parent_exist_with_child_and_no_booking_attached_EXPECT_parent_with_child_object_returned_from_service() {

        givenThereIsAParentAndChild();

        Optional<Parent> foundParent = _serviceUnderTest.getParent("P1478");

        assertThat(foundParent).isPresent();
        assertThat(foundParent.get().getExternalId()).isEqualTo("P1478");

        assertThat(foundParent.get().getChildren()).hasSize(1);
        assertThat(foundParent.get().getChildren().get(0).getExternalId()).isEqualTo("C7895");

        assertThat(foundParent.get().getChildren().get(0).getBookings()).isNull();
    }

    @Test
    void GIVEN_parent_exist_with_no_child_and_no_booking_attached_EXPECT_only_parent_object_returned_from_service() {

        givenThereIsAParent();

        Optional<Parent> foundParent = _serviceUnderTest.getParent("P1478");

        assertThat(foundParent).isPresent();
        assertThat(foundParent.get().getExternalId()).isEqualTo("P1478");

        assertThat(foundParent.get().getChildren()).isNull();
    }

    private void givenThereIsAParentAndChildAndBooking() {
        final Parent parent = TestDataBuilder.getTestParentObjectWithChildAndAssociatedBooking();

        when(_mockParentRepository.findByExternalId("P1478")).thenReturn(Optional.of(parent));
    }

    private void givenThereIsAParentAndChild() {
        final Parent parent = TestDataBuilder.getTestParentObjectWithChildButNoAssociatedBooking();

        when(_mockParentRepository.findByExternalId("P1478")).thenReturn(Optional.of(parent));
    }

    private void givenThereIsAParent() {
        final Parent parent = TestDataBuilder.getParent();

        when(_mockParentRepository.findByExternalId("P1478")).thenReturn(Optional.of(parent));
    }

	@Test
	public void GIVEN_valid_parent_and_child_domain_data_already_exist_EXPECT_parent_child_updated() {

		CreateParentInput createParentInput = givenThereIsParentAndChild();

		Parent parent = _serviceUnderTest.createOrUpdateParent(createParentInput);

		assertThat(parent).isNotNull();
		assertThat(parent.getChildren()).isNotEmpty();
		assertThat(parent.getExternalId()).isEqualTo("P1478");
		assertThat(parent.getChildren().get(0).getCapLevel()).isEqualTo(CapLevel.BLUE);
	}
	
	private CreateParentInput givenThereIsParentAndChild() {
		
		CreateParentInput createParentInput = generateTestParentObjectWithChild();
		
		Optional<Parent> parentDomainObject = generateParentChildDomainData(createParentInput);
		Parent parent = parentDomainObject.get();
		List<Child> childs = parent.getChildren();
		
		when(_mockParentRepository.findByExternalId(createParentInput.getUniqueExternalId())).thenReturn(parentDomainObject);
		when(_mockParentRepository.save(parent)).thenReturn(parent);
		when(_mockChildRepository.saveAll(parent.getChildren())).thenReturn(childs);	
		
		return createParentInput;
	}

	@Test
	public void GIVEN_parent_and_child_domain_data_not_already_exist_EXPECT_parent_child_created() {

		CreateParentInput createParentInput = generateTestParentObjectWithChild();
		
		givenThereIsEmptyParent(createParentInput);
		givenThereIsParentChild(createParentInput);
		
		Parent parent = _serviceUnderTest.createOrUpdateParent(createParentInput);

		assertThat(parent).isNotNull();
		assertThat(parent.getChildren()).isNotEmpty();
		assertThat(parent.getExternalId()).isEqualTo("P1478");
		assertThat(parent.getChildren().get(0).getCapLevel()).isEqualTo(CapLevel.BLUE);
	}

	@Test
	public void GIVEN_parent_child_input_data_AND_no_external_id_of_child_EXPECT_update_parent_child_and_return_valid_child() {

		CreateParentInput createParentInput = generateTestParentObjectWithChild();

		givenThereIsEmptyParent(createParentInput);
		givenThereIsParentAndChild();
		
		String errorMessage  = assertThrows(IllegalArgumentException.class,
				()->_serviceUnderTest.getAssociatedChildren(null, createParentInput)).getMessage();

		assertThat(errorMessage).isEqualTo("Could not find child with externalChildId 'null', did you already create that record?");
	}

	@Test
	public void GIVEN_no_parent_child_input_data_AND_external_id_of_child_EXPECT_to_return_valid_child() {

		givenThereIsChild();
		
		List<Child> children = _serviceUnderTest.getAssociatedChildren("CHD001", null);

		assertThat(children.get(0).getExternalId()).isEqualTo("CHD001");
		assertThat(children.get(0).getParent().getExternalId()).isEqualTo("P1478");
		assertThat(children.get(0).getCapLevel()).isEqualTo(CapLevel.BLUE);
	}

	private void givenThereIsChild() {
		
		Optional<Parent> parentDomainObject = generateParentChildDomainData(generateTestParentObjectWithChild());

		List<Child> childs = parentDomainObject.get().getChildren();
		when(_mockChildRepository.findByExternalId("CHD001")).thenReturn(Optional.of(childs.get(0)));
	}

	@Test
	public void GIVEN_no_parent_child_input_data_AND_no_external_id_of_child_EXPECT_exception_is_thrown() {

		String errorMessage  = assertThrows(IllegalArgumentException.class,
				()-> _serviceUnderTest.getAssociatedChildren(null, null)).getMessage();

		assertThat(errorMessage).isEqualTo("Neither existing externalChildId found nor createParentInput found for creating Parent and Child");
	}
	
	@Test
	public void GIVEN_valid_parent_and_child_input_data_AND_external_child_id_EXPECT_list_of_child_is_returned() {
		CreateParentInput createParentInput = generateTestParentObjectWithChild();

		givenThereIsAParentAndChildAndBooking();
		givenThereIsParentChild(createParentInput);
		
		List<Child> children = _serviceUnderTest.getAssociatedChildren("CHD001", createParentInput);

		assertThat(children.get(0).getExternalId()).isEqualTo("CHD001");
		assertThat(children.get(0).getParent().getExternalId()).isEqualTo("P1478");
		assertThat(children.get(0).getCapLevel()).isEqualTo(CapLevel.BLUE);
	}
	
	private void givenThereIsEmptyParent(CreateParentInput createParentInput) {
		Optional<Parent> emptyParentDomainObject = Optional.ofNullable(null);
		when(_mockParentRepository.findByExternalId(createParentInput.getUniqueExternalId())).thenReturn(emptyParentDomainObject);		
	}

	private void givenThereIsParentChild(CreateParentInput createParentInput) {
		
		Optional<Parent> parentDomainObject = generateParentChildDomainData(createParentInput);

		Parent parent = parentDomainObject.get();
		List<Child> children = parentDomainObject.get().getChildren();
		
		when(_mockParentRepository.save(parent)).thenReturn(parent);
		when(_mockChildRepository.saveAll(children)).thenReturn(children);	
	}

	private Optional<Parent> generateParentChildDomainData(CreateParentInput createParentInput) {

    	List<Child> childs = Arrays.asList(Child.builder()
    			.id(1L)
    			.externalId(createParentInput.getChildren().get(0).getUniqueExternalId())
    			.displayName(createParentInput.getChildren().get(0).getDisplayName())
    			.capLevel(CapLevel.BLUE)
    			.build()); 

    	Parent parent = Parent.builder()
    			.displayName(createParentInput.getDisplayName())
    			.externalId(createParentInput.getUniqueExternalId())
    			.children(childs).build();

		childs.get(0).setParent(parent);

		return Optional.ofNullable(parent);
    }

	private CreateParentInput generateTestParentObjectWithChild() {

		CreateChildInput createChildInput = CreateChildInput.newBuilder()
				.capLevel(CapLevel.BLUE)
				.displayName("Child_01_01")
				.dateOfBirth(LocalDate.of(2002, 12, 12))
				.uniqueExternalId("CHD001")
				.build();

		List<CreateChildInput> createChildInputs = Arrays.asList(createChildInput);

		return CreateParentInput.newBuilder()
				.displayName("Parent_01")
				.givenName("ParentGiveName")
				.familyName("ParentFamilyName")
				.uniqueExternalId("P1478")
				.children(createChildInputs)
				.build();
	}
	
	
}
